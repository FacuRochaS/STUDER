import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import {
  UserResponseDTO,
  LoginRequestDTO,
  UserCreateRequestDTO
} from '../../features/users/user.model';
import { AuthApiService } from './auth-api.service';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly userSubject = new BehaviorSubject<UserResponseDTO | null>(null);
  readonly user$ = this.userSubject.asObservable();
  readonly isAuthenticated$ = this.user$.pipe(map(u => !!u));

  constructor(
    private authApi: AuthApiService,
    private tokenStorage: TokenStorageService
  ) {}

  init(): Observable<UserResponseDTO | null> {
    if (!this.tokenStorage.hasValidToken()) {
      this.tokenStorage.clear();
      this.userSubject.next(null);
      return of(null);
    }
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

  login(data: LoginRequestDTO): Observable<UserResponseDTO | null> {
    return this.authApi.login(data).pipe(
      tap(res => this.tokenStorage.setAccessToken(res.accessToken)),
      switchMap(() => this.authApi.me()),
      tap(user => this.userSubject.next(user)),
      catchError(() => {
        this.tokenStorage.clear();
        this.userSubject.next(null);
        return of(null);
      })
    );
  }

  refresh(): Observable<boolean> {
    return this.authApi.refresh().pipe(
      tap(res => this.tokenStorage.setAccessToken(res.accessToken)),
      map(() => true),
      catchError(() => {
        this.tokenStorage.clear();
        this.userSubject.next(null);
        return of(false);
      })
    );
  }

  logout(): Observable<void> {
    this.tokenStorage.clear();
    this.userSubject.next(null);
    return this.authApi.logout();
  }
}
