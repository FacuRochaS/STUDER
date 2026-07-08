import { Injectable, Type } from '@angular/core';

export interface ContentTypeRegistration {
  creatorComponent: Type<any>;
  viewerComponent: Type<any>;
}

@Injectable({
  providedIn: 'root'
})
export class ContentRegistryService {
  private readonly registry = new Map<string, ContentTypeRegistration>();

  register(type: string, registration: ContentTypeRegistration): void {
    if (this.registry.has(type)) {
      console.warn(`Content type '${type}' is already registered. Overwriting.`);
    }
    this.registry.set(type, registration);
  }

  getViewerComponent(type: string): Type<any> | undefined {
    return this.registry.get(type)?.viewerComponent;
  }

  getCreatorComponent(type: string): Type<any> | undefined {
    return this.registry.get(type)?.creatorComponent;
  }

  getRegisteredTypes(): string[] {
    return Array.from(this.registry.keys());
  }
}
