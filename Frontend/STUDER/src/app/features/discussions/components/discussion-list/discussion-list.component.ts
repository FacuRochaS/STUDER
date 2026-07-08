import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DiscussionResponseDTO } from '../../discussion.model';
import { DiscussionItemComponent } from '../discussion-item/discussion-item.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-discussion-list',
  standalone: true,
  imports: [CommonModule, DiscussionItemComponent, TranslateModule],
  templateUrl: './discussion-list.component.html',
  styleUrls: ['./discussion-list.component.css']
})
export class DiscussionListComponent {
  @Input() discussions: DiscussionResponseDTO[] = [];

  trackByDiscussionId(index: number, discussion: DiscussionResponseDTO): number {
    return discussion.id;
  }

  onFavouriteToggled(event: { id: number, favourite: boolean }): void { // Corregido a 'favourite'
    const discussion = this.discussions.find(d => d.id === event.id);
    if (discussion) {
      discussion.favourite = event.favourite; // Corregido a 'favourite'
    }
  }
}
