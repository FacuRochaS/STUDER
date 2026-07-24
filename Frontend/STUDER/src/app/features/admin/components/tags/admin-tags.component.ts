import { Component, Input, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

const C = {
  primary: '#4285f4', green: '#00bf63', purple: '#7E57C2', orange: '#ff751f',
};

@Component({ selector: 'studer-admin-tags', standalone: true, imports: [CommonModule, TranslateModule],
  templateUrl: './admin-tags.component.html', styleUrls: ['./admin-tags.component.css']
})
export class AdminTagsComponent implements AfterViewInit {
  @Input() data: any = {};
  private charts: any[] = [];
  heatCells: any[] = [];

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

    this.hbar('g1', cd.topTags ?? [], { y: 'tag', x: 'usages' }, C.purple);
    this.line('g2', cd.tagsCreated ?? [], { x: 'date', y: 'tags' }, C.primary);
    this.line('g3', cd.tagGrowth ?? [], { x: 'date', y: 'usages' }, C.green);
    this.buildHeatmap(cd.tagCoOccurrence ?? []);
  }

  private el(id: string): HTMLCanvasElement | null { return document.getElementById(id) as HTMLCanvasElement; }

  private hbar(id: string, d: any[], axes: { y: string; x: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'bar', data: { labels: d.map(r => r[axes.y]), datasets: [{ data: d.map(r => r[axes.x]), backgroundColor: color, borderRadius: 4 }] }, options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, y: { grid: { display: false }, ticks: { font: { size: 9 } } } } } }));
  }

  private line(id: string, d: any[], axes: { x: string; y: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'line', data: { labels: d.map(r => r[axes.x]), datasets: [{ data: d.map(r => r[axes.y]), borderColor: color, backgroundColor: color + '22', fill: true, tension: 0.3, pointRadius: 1, borderWidth: 2 }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, x: { ticks: { maxTicksLimit: 12, font: { size: 9 } }, grid: { display: false } } } } }));
  }

  private buildHeatmap(cooc: any[]): void {
    if (!cooc?.length) { this.heatCells = []; return; }
    const tags = [...new Set([...cooc.map((r: any) => r.tag1), ...cooc.map((r: any) => r.tag2)])].slice(0, 12) as string[];
    const max = Math.max(...cooc.map((r: any) => r.occurrences), 1);
    const cells: any[] = [];
    for (const t1 of tags) {
      for (const t2 of tags) {
        const pair = cooc.find((r: any) => (r.tag1 === t1 && r.tag2 === t2) || (r.tag1 === t2 && r.tag2 === t1));
        cells.push({ t1, t2, val: pair?.occurrences ?? 0, pct: ((pair?.occurrences ?? 0) / max) * 100 });
      }
    }
    this.heatCells = cells;
  }
}
