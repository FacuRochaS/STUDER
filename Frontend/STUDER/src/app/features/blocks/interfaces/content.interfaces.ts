export interface BlockContentItem<T extends ContentDataItem = ContentDataItem> {
  id: string; // UUID
  type: string;
  data: T;
}

export type ContentDataItem = TextContentData | ActivityContentData | GalleryContentData | YoutubeContentData | ChartContentData;

// --- Text Content ---
export interface TextContentData {
  elements: TextElement[];
}

export type TextElement = ParagraphElement; // Future: HeadingElement, ListElement, etc.

export interface ParagraphElement {
  type: 'paragraph';
  runs: TextRun[];
}

export interface TextRun {
  text: string;
  bold?: boolean;
  italic?: boolean;
  underline?: boolean;
  strike?: boolean;
  code?: boolean;
  color?: string; // Theme color variable, e.g., 'primary', 'on-surface'
  background?: string | null; // Theme color variable
  link?: string | null;
}

// --- Placeholder Interfaces for Future Content Types ---
export interface ActivityContentData {
  // To be defined
}

export interface GalleryContentData {
  // To be defined
}

export interface YoutubeContentData {
  // To be defined
}

export interface ChartContentData {
  // To be defined
}
