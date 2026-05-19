import { Injectable } from '@angular/core';

type JwtPayload = { sub?: string; exp?: number; iat?: number; [k: string]: any };

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const payload = token.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');

    const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, '=');
    return JSON.parse(atob(padded));
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly tokenKey = 'studer_token';

  setAccessToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  clear(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getPayload(): JwtPayload | null {
    const token = this.getAccessToken();
    return token ? decodeJwtPayload(token) : null;
  }


  isExpired(leewaySeconds = 10): boolean {
    const payload = this.getPayload();
    const exp = payload?.exp;
    if (!exp) return true;
    const nowSec = Math.floor(Date.now() / 1000);
    return exp <= (nowSec + leewaySeconds);
  }

  hasValidToken(): boolean {
    const token = this.getAccessToken();
    return !!token && !this.isExpired();
  }
}
