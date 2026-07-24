import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { TextContentData, ParagraphData, TextRunData, TextAlign, TextSize, TextColor } from '../../interfaces/content.interfaces';
import { UploadService } from '../../../../core/services/upload.service';

@Component({
  selector: 'studer-text-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './text-creator.component.html',
  styleUrls: ['./text-creator.component.css']
})
export class TextCreatorComponent implements OnInit {
  @Input() data!: TextContentData;
  @Output() dataChange = new EventEmitter<TextContentData>();

  @ViewChild('editableArea', { static: true }) editableArea!: ElementRef<HTMLDivElement>;

  private readonly cssVarMap: Record<TextColor, string> = {
    primary: 'var(--color-text-prim)',
    secondary: 'var(--color-text-secu)',
    blue: 'var(--color-primary)',
    green: 'var(--color-correct)',
    orange: 'var(--color-warning)',
    red: 'var(--color-error)',
    purple: 'var(--color-info)'
  };

  private readonly hexMap: Record<TextColor, string> = {
    primary: '#171717',
    secondary: '#4e4e4e',
    blue: '#4285f4',
    green: '#00bf63',
    orange: '#ff751f',
    red: '#ff3131',
    purple: '#7E57C2'
  };

  private readonly sizeMap: Record<TextSize, string> = {
    small: '1',
    medium: '3',
    large: '5',
    xlarge: '7'
  };

  ngOnInit(): void {
    if (this.data && this.data.paragraphs && this.data.paragraphs.length > 0) {
      this.editableArea.nativeElement.innerHTML = this.convertJsonToHtml(this.data);
    } else {
      this.editableArea.nativeElement.innerHTML = '<p><br></p>';
    }
  }

  execute(command: string, value: string = ''): void {
    document.execCommand(command, false, value);
    this.editableArea.nativeElement.focus();
    this.updateModel();
  }

  changeColor(event: Event): void {
    const type = (event.target as HTMLSelectElement).value as TextColor;
    if (type) {
      this.execute('foreColor', this.hexMap[type]);
    }
  }

  changeSize(event: Event): void {
    const type = (event.target as HTMLSelectElement).value as TextSize;
    if (type) {
      this.execute('fontSize', this.sizeMap[type]);
    }
  }

  private readonly uploadService = inject(UploadService);

  showLinkModal = false;
  showImageModal = false;
  linkUrl = '';
  imageUploading = false;
  private savedRange: Range | null = null;

  promptLink(): void {
    this.saveSelection();
    this.linkUrl = '';
    this.showLinkModal = true;
  }

  insertLink(): void {
    if (this.linkUrl) {
      this.restoreSelection();
      this.insertHtml(`<a href="${this.linkUrl}" target="_blank">${this.linkUrl}</a>`);
    }
    this.showLinkModal = false;
    this.editableArea.nativeElement.focus();
  }

  promptImage(): void {
    this.saveSelection();
    this.showImageModal = true;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.imageUploading = true;
    this.uploadService.uploadImage(file, 'blocks').subscribe({
      next: (res) => {
        this.restoreSelection();
        this.insertHtml(`<img src="${res.url}" alt="image">`);
        this.imageUploading = false;
        this.showImageModal = false;
        this.editableArea.nativeElement.focus();
        input.value = '';
      },
      error: () => {
        this.imageUploading = false;
      }
    });
  }

  private saveSelection(): void {
    const sel = window.getSelection();
    if (sel && sel.rangeCount > 0) {
      this.savedRange = sel.getRangeAt(0).cloneRange();
    }
  }

  private restoreSelection(): void {
    const sel = window.getSelection();
    if (sel && this.savedRange) {
      sel.removeAllRanges();
      sel.addRange(this.savedRange);
    } else if (this.editableArea) {
      this.editableArea.nativeElement.focus();
    }
  }

  private insertHtml(html: string): void {
    const sel = window.getSelection();
    if (sel && sel.rangeCount > 0) {
      const range = sel.getRangeAt(0);
      if (this.editableArea.nativeElement.contains(range.commonAncestorContainer)) {
        range.deleteContents();
        const fragment = range.createContextualFragment(html);
        range.insertNode(fragment);
        range.collapse(false);
        this.editableArea.nativeElement.focus();
        this.updateModel();
        return;
      }
    }
    const el = this.editableArea.nativeElement;
    el.focus();
    el.insertAdjacentHTML('beforeend', html);
    this.updateModel();
  }

  // =========================================================================
  // CORE PARSER: DOM a JSON (Solución a los saltos de línea)
  // =========================================================================
  updateModel(): void {
    const root = this.editableArea.nativeElement;
    const paragraphs: ParagraphData[] = [];

    // Estado del párrafo actual que estamos construyendo
    let currentParagraph: ParagraphData = { align: 'left', runs: [] };

    // Función para "Cerrar" el párrafo actual y guardarlo en el array
    const commitParagraph = () => {
      if (currentParagraph.runs.length > 0) {
        paragraphs.push({ ...currentParagraph, runs: [...currentParagraph.runs] });
        currentParagraph.runs = [];
      }
    };

    // Función exclusiva para los <br>. Forzamos guardar la línea, aunque esté vacía
    const commitBr = () => {
      paragraphs.push({ ...currentParagraph, runs: [...currentParagraph.runs] });
      currentParagraph.runs = [];
    };

    // Detecta si la etiqueta HTML funciona como bloque separador
    const isBlock = (node: Node) => {
      if (node.nodeType !== Node.ELEMENT_NODE) return false;
      const tag = (node as HTMLElement).tagName;
      return ['P', 'DIV', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'LI', 'BLOCKQUOTE', 'TR', 'TD'].includes(tag);
    };

    const getAlignment = (el: HTMLElement): TextAlign | null => {
      if (el.style.textAlign) return el.style.textAlign as TextAlign;
      if (el.getAttribute('align')) return el.getAttribute('align') as TextAlign;
      return null;
    };

    // Algoritmo de recorrido en profundidad (DFS)
    const traverse = (node: Node, currentFormat: Omit<TextRunData, 'text'>) => {
      // 1. Si es texto, lo agregamos al párrafo actual
      if (node.nodeType === Node.TEXT_NODE) {
        // Limpiamos los saltos de línea residuales del HTML para no crear runs fantasma
        const text = node.textContent?.replace(/[\n\r]/g, '') || '';
        if (text !== '') {
          currentParagraph.runs.push({ ...currentFormat, text: text, imageUrl: null });
        }
        return;
      }

      // 2. Si es una etiqueta HTML...
      if (node.nodeType === Node.ELEMENT_NODE) {
        const el = node as HTMLElement;
        const tag = el.tagName;

        // Si encontramos un salto de línea explicito, cortamos el párrafo
        if (tag === 'BR') {
          commitBr();
          return;
        }

        // Si es una imagen
        if (tag === 'IMG') {
          const src = el.getAttribute('src') || (el as any).src || '';
          currentParagraph.runs.push({ ...currentFormat, text: '', imageUrl: src || null });
          return;
        }

        const block = isBlock(node);
        let prevAlign = currentParagraph.align;

        // Si es un div o p, cerramos lo anterior y tomamos su alineación
        if (block) {
          commitParagraph();
          const align = getAlignment(el);
          if (align) currentParagraph.align = align;
        }

        // Acumulamos formatos si encontramos etiquetas de estilo
        const nextFormat = { ...currentFormat };
        if (tag === 'B' || tag === 'STRONG') nextFormat.bold = true;
        if (tag === 'I' || tag === 'EM') nextFormat.italic = true;
        if (tag === 'U') nextFormat.underline = true;
        if (tag === 'S' || tag === 'STRIKE' || tag === 'DEL') nextFormat.strikethrough = true;
        if (tag === 'A') nextFormat.link = el.getAttribute('href');

        if (tag === 'FONT') {
          const colorAttr = el.getAttribute('color');
          if (colorAttr) nextFormat.color = this.matchColorType(colorAttr);
          const sizeAttr = el.getAttribute('size');
          if (sizeAttr) nextFormat.size = this.matchSizeType(sizeAttr);
        }
        if (el.style.color) nextFormat.color = this.matchColorType(el.style.color);

        // Procesamos los hijos recursivamente
        el.childNodes.forEach(child => traverse(child, nextFormat));

        // Al terminar el bloque, lo cerramos
        if (block) {
          commitParagraph();
          currentParagraph.align = prevAlign; // Restauramos la alineación anterior por si acaso
        }
      }
    };

    // Iniciamos el recorrido desde la raíz
    Array.from(root.childNodes).forEach(child => traverse(child, this.createDefaultRun()));

    // Guardamos cualquier texto residual que haya quedado al final
    commitParagraph();

    this.dataChange.emit({ paragraphs });
  }

  // =========================================================================

  private convertJsonToHtml(data: TextContentData): string {
    if (!data.paragraphs) return '';

    return data.paragraphs.map(p => {
      const alignAttr = p.align !== 'left' ? ` style="text-align: ${p.align};"` : '';

      const runsContent = p.runs.map(run => {
        if (run.imageUrl) return `<img src="${run.imageUrl}">`;

        let nodeHtml = run.text;
        if (run.bold) nodeHtml = `<strong>${nodeHtml}</strong>`;
        if (run.italic) nodeHtml = `<em>${nodeHtml}</em>`;
        if (run.underline) nodeHtml = `<u>${nodeHtml}</u>`;
        if (run.strikethrough) nodeHtml = `<s>${nodeHtml}</s>`;

        let styles = '';
        if (run.color && run.color !== 'primary') {
          styles += `color: ${this.cssVarMap[run.color]};`;
        }

        if (styles) nodeHtml = `<span style="${styles}">${nodeHtml}</span>`;
        if (run.link) nodeHtml = `<a href="${run.link}" target="_blank">${nodeHtml}</a>`;

        return nodeHtml;
      }).join('');

      // Si el párrafo no tiene runs, metemos un <br> para que no colapse visualmente en el editor
      return `<p${alignAttr}>${runsContent || '<br>'}</p>`;
    }).join('');
  }

  private matchColorType(colorString: string): TextColor {
    const normalized = colorString.toLowerCase().replace(/\s/g, '');

    if (normalized.includes('var(--color-text-prim)') || normalized.includes('#171717') || normalized.includes('rgb(23,23,23)')) return 'primary';
    if (normalized.includes('var(--color-text-secu)') || normalized.includes('#4e4e4e') || normalized.includes('rgb(78,78,78)')) return 'secondary';
    if (normalized.includes('var(--color-primary)') || normalized.includes('#4285f4') || normalized.includes('rgb(66,133,244)')) return 'blue';
    if (normalized.includes('var(--color-correct)') || normalized.includes('#00bf63') || normalized.includes('rgb(0,191,99)')) return 'green';
    if (normalized.includes('var(--color-warning)') || normalized.includes('#ff751f') || normalized.includes('rgb(255,117,31)')) return 'orange';
    if (normalized.includes('var(--color-error)') || normalized.includes('#ff3131') || normalized.includes('rgb(255,49,49)')) return 'red';
    if (normalized.includes('var(--color-info)') || normalized.includes('#7e57c2') || normalized.includes('rgb(126,87,194)')) return 'purple';

    return 'primary';
  }

  private matchSizeType(sizeStr: string): TextSize {
    if (sizeStr === '1') return 'small';
    if (sizeStr === '5') return 'large';
    if (sizeStr === '7') return 'xlarge';
    return 'medium';
  }

  private createDefaultRun(): Omit<TextRunData, 'text'> {
    return {
      imageUrl: null, link: null,
      bold: false, italic: false, underline: false, strikethrough: false,
      size: 'medium', color: 'primary'
    };
  }
}
