import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockCardComponent } from '../blocks/component/block-card/block-card.component';
import { BlockDetailComponent } from '../blocks/component/block-detail/block-detail.component';
import { BlockEditorComponent } from '../blocks/editor/block-editor.component';
import { BlockResponseDTO, BlockCompleteResponseDTO } from '../blocks/block.model';
import { BlockContentItem } from '../blocks/interfaces/content.interfaces';

@Component({
  selector: 'studer-test',
  standalone: true,
  imports: [
    CommonModule,
    BlockCardComponent,
    BlockDetailComponent,
    BlockEditorComponent,
  ],
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent {
  mockUser = {
    id: 1,
    username: 'testuser',
    firstName: 'Test',
    lastName: 'User',
    profilePictureAvatarUrl: 'https://via.placeholder.com/150',
    profilePictureOriginalUrl: '',
    profilePictureThumbnailUrl: '',
    profilePictureWebpUrl: ''
  };

  mockTextContent: BlockContentItem[] = [
    {
      id: 'uuid1',
      type: 'text',
      data: {
        elements: [
          {
            type: 'paragraph',
            runs: [
              { text: 'This is a ' },
              { text: 'bold', bold: true },
              { text: ' paragraph with ' },
              { text: 'italic', italic: true },
              { text: ' and ' },
              { text: 'code', code: true },
              { text: '.' }
            ]
          },
          {
            type: 'paragraph',
            runs: [
              { text: 'This is a second paragraph with a ' },
              { text: 'link to Google', link: 'https://google.com' },
              { text: '.' }
            ]
          }
        ]
      }
    }
  ];

  mockBlockCard: BlockResponseDTO = {
    id: 101,
    createdDatetime: new Date().toISOString(),
    lastUpdatedDatetime: new Date().toISOString(),
    owner: this.mockUser,
    isFork: true,
    name: 'Mocked Block Card',
    slug: 'mocked-block-card',
    difficulty: 'HARD',
    tags: ['mock', 'test', 'card'],
    version: {
      id: 201,
      createdDatetime: new Date().toISOString(),
      lastUpdatedDatetime: new Date().toISOString(),
      content: JSON.stringify(this.mockTextContent),
      versionNumber: 1,
      changeDescription: 'Initial version'
    }
  };

  mockBlockDetail: BlockCompleteResponseDTO = {
    id: 102,
    createdDatetime: new Date().toISOString(),
    lastUpdatedDatetime: new Date().toISOString(),
    owner: this.mockUser,
    isFork: false,
    name: 'Mocked Block for Detail View',
    slug: 'mocked-block-detail',
    difficulty: 'EASY',
    tags: ['mock', 'detail', 'testing'],
    versions: [
      {
        id: 301,
        createdDatetime: new Date(Date.now() - 86400000 * 2).toISOString(),
        lastUpdatedDatetime: new Date().toISOString(),
        content: JSON.stringify([{ id: 'v1', type: 'text', data: { elements: [{ type: 'paragraph', runs: [{ text: 'Content of version 1.' }] }] } }]),
        versionNumber: 1,
        changeDescription: 'First commit'
      },
      {
        id: 302,
        createdDatetime: new Date(Date.now() - 86400000).toISOString(),
        lastUpdatedDatetime: new Date().toISOString(),
        content: JSON.stringify([{ id: 'v2', type: 'text', data: { elements: [{ type: 'paragraph', runs: [{ text: 'Content of version 2, with some changes.' }] }] } }]),
        versionNumber: 2,
        changeDescription: 'Added more details'
      },
      {
        id: 303,
        createdDatetime: new Date().toISOString(),
        lastUpdatedDatetime: new Date().toISOString(),
        content: JSON.stringify(this.mockTextContent),
        versionNumber: 3,
        changeDescription: 'Final touches and formatting'
      }
    ],
    parent: null as any // No parent for this mock
  };
}
