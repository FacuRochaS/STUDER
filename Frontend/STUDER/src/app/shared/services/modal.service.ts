import { Injectable, ApplicationRef, createComponent, EnvironmentInjector, ComponentRef, Output, EventEmitter } from '@angular/core';
import { ModalComponent } from '../components/modal/modal.component';

@Injectable({
  providedIn: 'root'
})
export class ModalService {
  private modalComponentRef?: ComponentRef<ModalComponent>;

  constructor(
    private appRef: ApplicationRef,
    private injector: EnvironmentInjector
  ) {}

  open(
    component: any,
    options?: {
      title?: string;
      inputs?: { [key: string]: any };
      outputs?: { [key: string]: (event: any) => void };
    }
  ): void {
    if (this.modalComponentRef) {
      this.close();
    }

    const modalComponent = createComponent(ModalComponent, {
      environmentInjector: this.injector,
    });

    modalComponent.instance.title = options?.title || '';
    modalComponent.instance.childComponent = component;
    modalComponent.instance.inputs = options?.inputs || {};
    modalComponent.instance.outputs = options?.outputs || {};
    modalComponent.instance.close.subscribe(() => this.close());

    document.body.appendChild(modalComponent.location.nativeElement);
    this.appRef.attachView(modalComponent.hostView);
    this.modalComponentRef = modalComponent;
  }

  close(): void {
    if (this.modalComponentRef) {
      this.appRef.detachView(this.modalComponentRef.hostView);
      this.modalComponentRef.destroy();
      this.modalComponentRef = undefined;
    }
  }
}
