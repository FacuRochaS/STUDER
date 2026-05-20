import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { UserResponseDTO } from '../users/user.model';
import { LoaderComponent } from '../../shared/components/loader/loader.component';

@Component({
  selector: 'studer-home',
  standalone: true,
  imports: [CommonModule, TranslatePipe, LoaderComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  user: UserResponseDTO | null = null;

  constructor(private readonly authState: AuthStateService) {}

  ngOnInit(): void {
    this.authState.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(u => this.user = u);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

