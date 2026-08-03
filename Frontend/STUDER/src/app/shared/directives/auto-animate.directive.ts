import { Directive, ElementRef, inject, AfterViewInit, Input } from '@angular/core';
import autoAnimate, { AutoAnimateOptions } from '@formkit/auto-animate';

@Directive({
  selector: '[appAutoAnimate]',
  standalone: true
})
export class AutoAnimateDirective implements AfterViewInit {
  private el = inject(ElementRef);

  // Aceptamos el objeto de configuración O un string vacío (que es lo que manda Angular por defecto)
  @Input('appAutoAnimate') options?: Partial<AutoAnimateOptions> | '';

  ngAfterViewInit(): void {
    // Si mandaron opciones y es un objeto real, lo usamos. Si no, usamos la configuración por defecto.
    const config = (this.options && typeof this.options !== 'string')
      ? this.options
      : {
        duration: 300,
        easing: 'ease-in-out',
        disrespectUserMotionPreference: true
      };

    autoAnimate(this.el.nativeElement, config);
  }
}
