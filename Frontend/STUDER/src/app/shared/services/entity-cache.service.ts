import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface EntityCacheEntry<T> {
  data: T;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class EntityCacheService {
  private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutos

  private userCache = new Map<string, EntityCacheEntry<any>>();
  private tagCache = new Map<string, EntityCacheEntry<any>>();
  private courseCache = new Map<string, EntityCacheEntry<any>>();
  private contestCache = new Map<string, EntityCacheEntry<any>>();
  private blockCache = new Map<string, EntityCacheEntry<any>>();

  constructor() {}

  /**
   * Obtiene un usuario del caché
   */
  getUser(username: string): any | null {
    return this.getCachedData(this.userCache, username);
  }

  /**
   * Guarda un usuario en el caché
   */
  setUser(username: string, data: any): void {
    this.setCachedData(this.userCache, username, data);
  }

  /**
   * Obtiene un tag del caché
   */
  getTag(tagName: string): any | null {
    return this.getCachedData(this.tagCache, tagName);
  }

  /**
   * Guarda un tag en el caché
   */
  setTag(tagName: string, data: any): void {
    this.setCachedData(this.tagCache, tagName, data);
  }

  /**
   * Obtiene un curso del caché
   */
  getCourse(courseName: string): any | null {
    return this.getCachedData(this.courseCache, courseName);
  }

  /**
   * Guarda un curso en el caché
   */
  setCourse(courseName: string, data: any): void {
    this.setCachedData(this.courseCache, courseName, data);
  }

  /**
   * Obtiene un contest del caché
   */
  getContest(contestName: string): any | null {
    return this.getCachedData(this.contestCache, contestName);
  }

  /**
   * Guarda un contest en el caché
   */
  setContest(contestName: string, data: any): void {
    this.setCachedData(this.contestCache, contestName, data);
  }

  /**
   * Obtiene un bloque del caché
   */
  getBlock(blockName: string): any | null {
    return this.getCachedData(this.blockCache, blockName);
  }

  /**
   * Guarda un bloque en el caché
   */
  setBlock(blockName: string, data: any): void {
    this.setCachedData(this.blockCache, blockName, data);
  }

  /**
   * Limpia todo el caché
   */
  clearCache(): void {
    this.userCache.clear();
    this.tagCache.clear();
    this.courseCache.clear();
    this.contestCache.clear();
    this.blockCache.clear();
  }

  /**
   * Obtiene datos del caché con validación de tiempo
   */
  private getCachedData(cache: Map<string, EntityCacheEntry<any>>, key: string): any | null {
    const entry = cache.get(key);
    if (!entry) return null;

    const now = Date.now();
    if (now - entry.timestamp > this.CACHE_DURATION) {
      cache.delete(key);
      return null;
    }

    return entry.data;
  }

  /**
   * Guarda datos en el caché
   */
  private setCachedData(cache: Map<string, EntityCacheEntry<any>>, key: string, data: any): void {
    cache.set(key, {
      data,
      timestamp: Date.now()
    });
  }
}

