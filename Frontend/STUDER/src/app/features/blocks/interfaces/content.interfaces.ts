export type BlockType = 'text' | 'activity' | 'video' | 'gallery';

export interface BlockContentItem<T = unknown> {
  id: string;
  type: BlockType;
  data: T;
}
export interface ActivityOption {
  id: string;
  text: string;           // El texto de la opción (SIMPLE)
  imageUrl: string | null; // Imagen opcional adjunta a la opción

  // Propiedades específicas según el activityType:
  isCorrect: boolean | null;   // M.Choice: ¿Es la correcta?
  matchText: string | null;    // Matching: El texto con el que hace pareja
  orderIndex: number | null;   // Ordering: Su posición correcta (1, 2, 3...)
}

export type TextAlign = 'left' | 'center' | 'right' | 'justify';

export type TextSize = 'small' | 'medium' | 'large' | 'xlarge';

export type TextColor = 'primary' | 'secondary' | 'blue' | 'green' | 'orange' | 'red' | 'purple';

export interface TextContentData {
  paragraphs: ParagraphData[];
}

export interface ParagraphData {
  align: TextAlign;
  runs: TextRunData[];
}

export interface TextRunData {
  text: string;
  imageUrl: string | null;
  link: string | null;
  bold: boolean;
  italic: boolean;
  underline: boolean;
  strikethrough: boolean;
  size: TextSize;
  color: TextColor;
}

export type VideoPlatform = 'youtube' | 'vimeo' | 'native';

export interface VideoContentData {
  url: string;
  platform: VideoPlatform;
  startTime: number | null;
  autoplay: boolean;
  controls: boolean;
}

export type GalleryLayout = 'carousel' | 'grid' | 'masonry';

export interface GalleryImage {
  url: string;
  alt: string;
  caption: string | null;
}

export interface GalleryContentData {
  images: GalleryImage[];
  layout: GalleryLayout;
}

export type ActivityType = 'multiple_choice' | 'drag_and_drop' | 'matching' | 'ordering';

export type RichNodeType = 'text' | 'image' | 'code';

export interface RichNode {
  type: RichNodeType;
  value: string;
}


export interface ActivityContentData {
  activityType: ActivityType;
  statement: RichNode[];
  options: ActivityOption[];
  allowRetry: boolean;
  showFeedback: boolean;
}
