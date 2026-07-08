import { Component, Input, Type } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import {TranslatePipe} from '@ngx-translate/core';
import {TextViewerComponent} from '../../text/viewer/text-viewer.component';
import {ActivityViewerComponent} from '../../activity/viewer/activity-viewer.component';


@Component({
  selector: 'studer-block-viewer',
  standalone: true,
  imports: [
    CommonModule,
    TranslatePipe,
  ],
  templateUrl: './block-viewer.component.html',
  styleUrls: ['./block-viewer.component.css']
})
export class BlockViewerComponent {
  @Input() content: BlockContentItem[] = [];


  private readonly componentRegistry: Record<string, Type<any>> = {
    'text': TextViewerComponent,
     'activity': ActivityViewerComponent,
  };

  /**
   * Devuelve el componente correspondiente al tipo,
   * o null si no existe (puedes devolver un componente de "Error/No Soportado" si prefieres)
   */
  getComponentForType(type: string): Type<any> | null {
    const component = this.componentRegistry[type];
    if (!component) {
      console.warn(`No hay un visualizador registrado para el tipo de bloque: ${type}`);
      return null;
    }
    return component;
  }
}
