import { ContentRegistryService } from './content-registry.service';
import {TextCreatorComponent} from '../text/creator/text-creator.component';
import {TextViewerComponent} from '../text/viewer/text-viewer.component';
import { provideAppInitializer, inject } from '@angular/core';
import {ActivityCreatorComponent} from '../activity/creator/activity-creator.component';
import {ActivityViewerComponent} from '../activity/viewer/activity-viewer.component';


export function registerContent(registry: ContentRegistryService): void {
  registry.register('text', {
    creatorComponent: TextCreatorComponent,
    viewerComponent: TextViewerComponent,
  });
  registry.register('activity', {
    creatorComponent: ActivityCreatorComponent,
    viewerComponent: ActivityViewerComponent,
  });
  // Aquí agregarás los demás en el futuro:
  // registry.register('activity', { ... });
  // registry.register('video', { ... });
}


export function provideContentRegistry() {
  return provideAppInitializer(() => {

    const registry = inject(ContentRegistryService);
    registerContent(registry);
  });
}
