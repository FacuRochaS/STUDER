import { ContentRegistryService } from './registry/content-registry.service';
import { TextCreatorComponent } from './creators/text/text-creator.component';
import { TextViewerComponent } from './viewers/text/text-viewer.component';

export function registerContent(registry: ContentRegistryService): void {
  registry.register('text', {
    creatorComponent: TextCreatorComponent,
    viewerComponent: TextViewerComponent,
  });

  // Future content types will be registered here
}
