import { Routes } from '@angular/router';
import { LandingComponent } from './features/landing/landing.component';
import { LoginRegisterComponent } from './features/users/components/login-register/login-register.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { HomeComponent } from './features/home/home.component';
import { DiscussionListComponent } from './features/discussions/components/discussion-list/discussion-list.component';
import { DiscussionDetailComponent } from './features/discussions/components/discussion-detail/discussion-detail.component';
import { TestComponent } from './features/test/test.component';
import { authGuard } from './core/auth/auth.guard';
import { publicGuard } from './core/auth/public.guard';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginRegisterComponent},
  { path: 'test', component: TestComponent },
  {
    path: '',
    component: LayoutComponent,
    //canActivate: [authGuard],
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'discussions', component: DiscussionListComponent },
      { path: 'discussions/:id', component: DiscussionDetailComponent },
      { path: 'courses', component: HomeComponent },
      { path: 'calendar', component: HomeComponent },
      { path: 'messages', component: HomeComponent },
      { path: 'account', component: HomeComponent },
      { path: 'user/:username', component: HomeComponent },
    ]
  },
  { path: '**', redirectTo: '' }
];
