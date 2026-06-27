import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TextContentData } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-text-creator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './text-creator.component.html',
  styleUrls: ['./text-creator.component.css']
})
export class TextCreatorComponent implements OnInit {
  @Input() data: TextContentData = { elements: [] };
  @Output() dataChange = new EventEmitter<TextContentData>();

  // For simplicity, we'll start with a single textarea that maps to the first paragraph and run.
  // A real implementation would have a much richer editor UI.
  displayText = '';

  ngOnInit(): void {
    // Initialize displayText from the first paragraph/run if it exists
    if (this.data.elements?.[0]?.type === 'paragraph' && this.data.elements[0].runs?.[0]) {
      this.displayText = this.data.elements[0].runs[0].text;
    }
  }

  onTextChange(): void {
    // This is a simplified mapping. A real editor would update the model more granularly.
    const newData: TextContentData = {
      elements: [
        {
          type: 'paragraph',
          runs: [
            {
              text: this.displayText
            }
          ]
        }
      ]
    };
    this.dataChange.emit(newData);
  }
}
