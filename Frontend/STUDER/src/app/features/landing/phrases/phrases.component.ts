import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {NgClass} from '@angular/common';

@Component({
  selector: 'studer-phrases',
  standalone: true,
  templateUrl: './phrases.component.html',
  styleUrls: ['./phrases.component.css'],
  imports: [
    TranslatePipe,
    NgClass
  ]
})
export class PhrasesComponent implements OnInit, OnDestroy {
  @Input() phrases: string[] = [];
  @Input() colorClasses: string[] = [];
  interval: any;
  currentIndex = 0;
  fadeClass = 'fade-in';
  private isTransitioning = false;

  get currentPhrase() {
    return this.phrases[this.currentIndex % this.phrases.length];
  }
  get colorClass() {
    return this.colorClasses[this.currentIndex % this.colorClasses.length];
  }

  ngOnInit() {
    this.startRotation();
  }
  ngOnDestroy() {
    clearInterval(this.interval);
  }
  startRotation() {
    this.interval = setInterval(() => {
      if (this.isTransitioning) return;
      this.isTransitioning = true;
      this.fadeClass = 'fade-out';
      setTimeout(() => {
        this.currentIndex = (this.currentIndex + 1) % this.phrases.length;
        this.fadeClass = 'fade-in';
        setTimeout(() => {
          this.isTransitioning = false;
        }, 600);
      }, 600);
    }, 3500);
  }
}
