import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, finalize, map, of, shareReplay, switchMap, tap } from 'rxjs';
import {
  User,
  LoginRequestDTO,
  UserCreateRequestDTO
} from '../../features/users/user.model';
import { AuthApiService } from './auth-api.service';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly userSubject = new BehaviorSubject<User | null>(null);
  readonly user$ = this.userSubject.asObservable();
  readonly isAuthenticated$ = this.user$.pipe(map(u => !!u));

  private refreshInFlight$: Observable<boolean> | null = null;
  private refreshTimerId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private authApi: AuthApiService,
    private tokenStorage: TokenStorageService
  ) {}

  init(): Observable<User | null> {
    if (!this.tokenStorage.hasValidToken()) {
      this.tokenStorage.clear();
      this.userSubject.next(null);
      return of(null);
    }
    this.scheduleProactiveRefresh();
    return this.authApi.me().pipe(
      tap(user => this.userSubject.next(user)),
      catchError(() => {
        this.tokenStorage.clear();
        this.userSubject.next(null);
        return of(null);
      })
    );
  }

  register(data: UserCreateRequestDTO) {
    return this.authApi.register(data);
  }

  login(data: LoginRequestDTO): Observable<User | null> {
    return this.authApi.login(data).pipe(
      tap(res => { this.tokenStorage.setAccessToken(res.accessToken); this.scheduleProactiveRefresh(); }),
      switchMap(() => this.authApi.me()),
      tap(user => this.userSubject.next(user)),
      catchError(() => {
        this.tokenStorage.clear();
        this.userSubject.next(null);
        return of(null);
      })
    );
  }

  /**
   * Refreshes the access token. Concurrent callers (e.g. multiple parallel
   * 401 responses) share the same in-flight request so we never fire more
   * than one /auth/refresh call at a time - the backend rotates & revokes
   * the refresh token on every call, so parallel calls would otherwise
   * trigger reuse-detection and force a full logout.
   */
  refresh(): Observable<boolean> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }

    this.refreshInFlight$ = this.authApi.refresh().pipe(
      tap(res => { this.tokenStorage.setAccessToken(res.accessToken); this.scheduleProactiveRefresh(); }),
      map(() => true),
      catchError(() => {
        this.tokenStorage.clear();
        this.userSubject.next(null);
        return of(false);
      }),
      finalize(() => { this.refreshInFlight$ = null; }),
      shareReplay(1)
    );

    return this.refreshInFlight$;
  }

  logout(): Observable<void> {
    this.clearProactiveRefresh();
    this.tokenStorage.clear();
    this.userSubject.next(null);
    return this.authApi.logout();
  }

  /**
   * Schedules a background refresh shortly before the current access token
   * expires, so the user's session survives without ever hitting a 401.
   */
  private scheduleProactiveRefresh(): void {
    this.clearProactiveRefresh();
    const expiresInMs = this.tokenStorage.getExpiresInMs();
    if (expiresInMs <= 0) return;

    // Refresh 60s before expiry (or halfway through if the token TTL is very short).
    const bufferMs = Math.min(60000, expiresInMs / 2);
    const delay = Math.max(0, expiresInMs - bufferMs);

    this.refreshTimerId = setTimeout(() => {
      this.refresh().subscribe();
    }, delay);
  }

  private clearProactiveRefresh(): void {
    if (this.refreshTimerId !== null) {
      clearTimeout(this.refreshTimerId);
      this.refreshTimerId = null;
    }
  }
}
