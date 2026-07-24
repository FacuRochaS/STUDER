import { Component, Input, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

const C = {
  primary: '#4285f4', green: '#00bf63', red: '#ff3131', orange: '#ff751f',
  purple: '#7E57C2', teal: '#00bcd4',
};

@Component({ selector: 'studer-admin-courses', standalone: true, imports: [CommonModule, TranslateModule],
  templateUrl: './admin-courses.component.html', styleUrls: ['./admin-courses.component.css']
})
export class AdminCoursesComponent implements AfterViewInit {
  @Input() data: any = {};
  private charts: any[] = [];

  get cards(): any { return this.data?.cards ?? {}; }
  get chartsData(): any { return this.data?.charts ?? {}; }
  ck(k: string): number { return this.cards[k] ?? 0; }
  cs(k: string): string { return this.cards[k] ?? ''; }

  ngAfterViewInit(): void { setTimeout(() => this.tryRender(), 400); }
  private tryRender(): void { if (this.data?.charts) { this.render(); return; } setTimeout(() => this.tryRender(), 200); }
  private destroy(): void { this.charts.forEach(c => { try { c.destroy(); } catch {} }); this.charts = []; }

  render(): void {
    this.destroy();
    const cd = this.chartsData;
    if (!cd) return;

    this.line('c1', cd.coursesCreated ?? [], { x: 'date', y: 'courses' }, C.green);
    this.histo('c2', cd.blocksPerCourse ?? [], 'blocksRange', 'courses', C.teal);
    this.difficulty('c3', cd.courseDifficulty);
    this.hbar('c4', cd.mostViewed ?? [], { y: 'course', x: 'views' }, C.primary);
    this.hbar('c5', cd.mostReused ?? [], { y: 'course', x: 'reusedBlocks' }, C.orange);
  }

  private el(id: string): HTMLCanvasElement | null { return document.getElementById(id) as HTMLCanvasElement; }

  private line(id: string, d: any[], axes: { x: string; y: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'line', data: { labels: d.map(r => r[axes.x]), datasets: [{ data: d.map(r => r[axes.y]), borderColor: color, backgroundColor: color + '22', fill: true, tension: 0.3, pointRadius: 1, borderWidth: 2 }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, x: { ticks: { maxTicksLimit: 12, font: { size: 9 } }, grid: { display: false } } } } }));
  }

  private histo(id: string, d: any[], xKey: string, yKey: string, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'bar', data: { labels: d.map(r => r[xKey]), datasets: [{ data: d.map(r => r[yKey]), backgroundColor: color, borderRadius: 4 }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, x: { grid: { display: false }, ticks: { font: { size: 9 } } } } } }));
  }

  private difficulty(id: string, dd: any): void {
    if (!dd) return; const e = this.el(id); if (!e) return;
    const keys = ['EASY','NORMAL','HARD','EXPERT'];
    const vals = keys.map(k => dd[k] ?? 0);
    if (vals.every(v => !v)) return;
    this.charts.push(new Chart(e, { type: 'doughnut', data: { labels: keys, datasets: [{ data: vals, backgroundColor: [C.green, C.primary, C.orange, C.red], borderWidth: 0 }] }, options: { responsive: true, maintainAspectRatio: false, cutout: '60%', plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, padding: 10, font: { size: 10 } } } } } }));
  }

  private hbar(id: string, d: any[], axes: { y: string; x: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'bar', data: { labels: d.map(r => r[axes.y]), datasets: [{ data: d.map(r => r[axes.x]), backgroundColor: color, borderRadius: 4 }] }, options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, y: { grid: { display: false }, ticks: { font: { size: 9 } } } } } }));
  }
}
