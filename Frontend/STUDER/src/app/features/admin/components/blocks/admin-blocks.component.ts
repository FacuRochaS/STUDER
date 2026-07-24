import { Component, Input, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

const C = {
  primary: '#4285f4', green: '#00bf63', red: '#ff3131', orange: '#ff751f',
  purple: '#7E57C2', teal: '#00bcd4', yellow: '#ffc107', dark: '#2b2b2e',
};

@Component({ selector: 'studer-admin-blocks', standalone: true, imports: [CommonModule, TranslateModule],
  templateUrl: './admin-blocks.component.html', styleUrls: ['./admin-blocks.component.css']
})
export class AdminBlocksComponent implements AfterViewInit {
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

    this.line('b1', cd.blocksCreated ?? [], { x: 'date', y: 'blocks' }, C.primary);
    this.histo('b2', cd.versionsDistribution ?? [], 'versionsRange', 'blocks', C.teal);
    this.histo('b3', cd.forksDistribution ?? [], 'forksRange', 'blocks', C.orange);
    this.difficulty('b4', cd.difficultyDistribution);
    this.types('b5', cd.blockTypes ?? []);
    this.hbar('b6', cd.mostForked ?? [], { y: 'block', x: 'forks' }, C.orange);
    this.hbar('b7', cd.mostLiked ?? [], { y: 'block', x: 'likes' }, C.red);
    this.hbar('b8', cd.topAuthors ?? [], { y: 'author', x: 'blocks' }, C.primary);
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

  private types(id: string, d: any[]): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    const colors = [C.primary, C.green, C.orange, C.red, C.purple, C.teal, C.yellow, C.dark];
    this.charts.push(new Chart(e, { type: 'doughnut', data: { labels: d.map(r => r.type), datasets: [{ data: d.map(r => r.count), backgroundColor: colors.slice(0, d.length), borderWidth: 0 }] }, options: { responsive: true, maintainAspectRatio: false, cutout: '60%', plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, padding: 10, font: { size: 10 } } } } } }));
  }

  private hbar(id: string, d: any[], axes: { y: string; x: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'bar', data: { labels: d.map(r => r[axes.y]), datasets: [{ data: d.map(r => r[axes.x]), backgroundColor: color, borderRadius: 4 }] }, options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, y: { grid: { display: false }, ticks: { font: { size: 9 } } } } } }));
  }
}
