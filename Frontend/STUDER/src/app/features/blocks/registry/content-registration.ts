import { ContentRegistryService } from './content-registry.service';
import {TextCreatorComponent} from '../text/creator/text-creator.component';
import {TextViewerComponent} from '../text/viewer/text-viewer.component';
import { provideAppInitializer, inject } from '@angular/core';
import {ActivityCreatorComponent} from '../activity/creator/activity-creator.component';
import {ActivityViewerComponent} from '../activity/viewer/activity-viewer.component';
import {VideoCreatorComponent} from '../video/creator/video-creator.component';
import {VideoViewerComponent} from '../video/viewer/video-viewer.component';
import {GalleryCreatorComponent} from '../gallery/creator/gallery-creator.component';
import {GalleryViewerComponent} from '../gallery/viewer/gallery-viewer.component';


export function registerContent(registry: ContentRegistryService): void {
  registry.register('text', {
    creatorComponent: TextCreatorComponent,
    viewerComponent: TextViewerComponent,
  });
  registry.register('activity', {
    creatorComponent: ActivityCreatorComponent,
    viewerComponent: ActivityViewerComponent,
  });
  registry.register('video', {
    creatorComponent: VideoCreatorComponent,
    viewerComponent: VideoViewerComponent,
  });
  registry.register('gallery', {
    creatorComponent: GalleryCreatorComponent,
    viewerComponent: GalleryViewerComponent,
  });
}


export function provideContentRegistry() {
  return provideAppInitializer(() => {

    const registry = inject(ContentRegistryService);
    registerContent(registry);
  });
}
