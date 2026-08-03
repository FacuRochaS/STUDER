import { Component, Input, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

const C = {
  primary: '#4285f4', green: '#00bf63', red: '#ff3131', orange: '#ff751f',
  purple: '#7E57C2', teal: '#00bcd4', user: '#4285f4', course: '#00bf63',
  block: '#ff9800', contest: '#ff3131', discussion: '#7E57C2', post: '#4285f4',
  message: '#ff751f',
};

const SECTIONS = ['posts','blocks','courses','discussions','messages'];
const SCOLORS: Record<string, string> = { posts: C.post, blocks: C.block, courses: C.course, discussions: C.discussion, messages: C.message };

@Component({
  selector: 'studer-admin-dashboard',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements AfterViewInit {
  @Input() data: any = {};
  private charts: any[] = [];
  heatCells: any[] = [];

  get chartsData(): any { return this.data?.charts ?? {}; }
  get chartData(): any { return this.data?.charts ?? {}; }
  ck(k: string): number { return this.data?.[k] ?? 0; }
  cs(k: string): string { return this.data?.[k] ?? ''; }

  getCell(sec: string, hour: number): number | null {
    const h = hour.toString();
    for (const cell of this.heatCells) {
      if (cell.hour === h && cell.section === sec) return cell.val;
    }
    return null;
  }

  cellVal(sec: string, hour: number): number { return this.getCell(sec, hour) ?? 0; }
  cellOpacity(sec: string, hour: number): string {
    if (!this.heatCells.length) return '0';
    const max = Math.max(...this.heatCells.map((c: any) => c.val), 1);
    const v = this.getCell(sec, hour) ?? 0;
    return String(Math.round((v / max) * 35) / 100);
  }

  ngAfterViewInit(): void { setTimeout(() => this.tryRender(), 400); }
  private tryRender(): void { if (this.data?.charts) { this.render(); return; } setTimeout(() => this.tryRender(), 200); }
  private destroy(): void { this.charts.forEach(c => { try { c.destroy(); } catch {} }); this.charts = []; }

  render(): void {
    this.destroy();
    const cd = this.chartsData;
    if (!cd) return;

    this.buildHeatmap(cd.activityHeatmap ?? []);
    this.peaks('ch2', cd.activityPeaks ?? []);
    this.doughnut('ch3', ['blocks','courses','posts','discussions','contests'].map(k => cd.contentDistribution?.[k] ?? 0),
      ['Blocks','Courses','Posts','Discussions','Contests'], [C.block, C.course, C.post, C.discussion, C.contest]);
    this.line('ch4', cd.usersOverTime ?? [], { x: 'month', y: 'usersRegistered' }, C.user);
    this.weekLine('ch5', cd.weeklyGrowth ?? [], ['users','blocks','courses','posts']);
    this.hbar('ch6', (cd.topTags ?? []).slice(0, 12), { y: 'tag', x: 'usages' }, C.purple);
  }

  private el(id: string): HTMLCanvasElement | null { return document.getElementById(id) as HTMLCanvasElement; }

  private buildHeatmap(d: any[]): void {
    if (!d?.length) { this.heatCells = []; return; }
    const max = Math.max(...d.map((r: any) => SECTIONS.reduce((s: number, k: string) => s + (r[k] || 0), 0)), 1);
    const cells: any[] = [];
    for (const row of d) {
      for (const sec of SECTIONS) {
        cells.push({ hour: row.hour, section: sec, val: row[sec] ?? 0, pct: ((row[sec] ?? 0) / max) * 100 });
      }
    }
    this.heatCells = cells;
  }

  private peaks(id: string, d: any[]): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'line', data: {
      labels: d.map(r => r.hour + ':00'),
      datasets: [
        { label: 'Posts', data: d.map(r => r.posts), borderColor: C.post, borderWidth: 1.5, tension: 0.3, fill: false, pointRadius: 2 },
        { label: 'Blocks', data: d.map(r => r.blocks), borderColor: C.block, borderWidth: 1.5, tension: 0.3, fill: false, pointRadius: 2 },
        { label: 'Courses', data: d.map(r => r.courses), borderColor: C.course, borderWidth: 1.5, tension: 0.3, fill: false, pointRadius: 2 },
        { label: 'Discussions', data: d.map(r => r.discussions), borderColor: C.discussion, borderWidth: 1.5, tension: 0.3, fill: false, pointRadius: 2 },
        { label: 'Messages', data: d.map(r => r.messages), borderColor: C.message, borderWidth: 1.5, tension: 0.3, fill: false, pointRadius: 2 },
      ]
    }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, padding: 12, font: { size: 9 } } } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, x: { grid: { display: false }, ticks: { font: { size: 9 } } } } } }));
  }

  private doughnut(id: string, v: number[], l: string[], c: string[]): void {
    if (!v?.length || v.every(x => !x)) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'doughnut', data: { labels: l, datasets: [{ data: v, backgroundColor: c, borderWidth: 0 }] }, options: { responsive: true, maintainAspectRatio: false, cutout: '60%', plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, padding: 10, font: { size: 10 } } } } } }));
  }

  private line(id: string, d: any[], axes: { x: string; y: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'line', data: { labels: d.map(r => r[axes.x]), datasets: [{ data: d.map(r => r[axes.y]), borderColor: color, backgroundColor: color + '22', fill: true, tension: 0.3, pointRadius: 1, borderWidth: 2 }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, x: { ticks: { maxTicksLimit: 12, font: { size: 9 } }, grid: { display: false } } } } }));
  }

  private hbar(id: string, d: any[], axes: { y: string; x: string }, color: string): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    this.charts.push(new Chart(e, { type: 'bar', data: { labels: d.map(r => r[axes.y]), datasets: [{ data: d.map(r => r[axes.x]), backgroundColor: color, borderRadius: 4 }] }, options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, y: { grid: { display: false }, ticks: { font: { size: 9 } } } } } }));
  }

  private weekLine(id: string, d: any[], series: string[]): void {
    if (!d?.length) return; const e = this.el(id); if (!e) return;
    const colors: Record<string, string> = { users: C.user, blocks: C.block, courses: C.course, posts: C.post };
    this.charts.push(new Chart(e, { type: 'line', data: { labels: d.map(r => r.week), datasets: series.map(s => ({ label: s, data: d.map(r => r[s] ?? 0), borderColor: colors[s], backgroundColor: colors[s] + '22', fill: false, tension: 0.3, pointRadius: 2, borderWidth: 2 })) }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, padding: 12, font: { size: 9 } } } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }, x: { grid: { display: false } } } } }));
  }
}
