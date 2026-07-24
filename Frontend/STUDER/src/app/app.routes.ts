import { Routes } from '@angular/router';
import { LandingComponent } from './features/landing/landing.component';
import { LoginRegisterComponent } from './features/users/components/login-register/login-register.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { HomeComponent } from './features/home/home.component';
import { DiscussionComponent } from './features/discussions/components/discussion.component';
import { TestComponent } from './features/test/test.component';
import { SearchComponent } from './features/search/search.component';
import { UserProfileComponent } from './features/users/components/user-profile/user-profile.component';
import { ChatComponent } from './features/chats/components/chat.component';
import { DiscussionsTestComponent } from './features/test/discussions-test.component';
import {authGuard} from './core/auth/auth.guard';
import { CourseExploreComponent } from './features/courses/components/course-explore/course-explore.component';
import { CourseCreateComponent } from './features/courses/components/course-create/course-create.component';
import { ContestListComponent } from './features/contest/components/contest-list/contest-list.component';
import { ContestDetailComponent } from './features/contest/components/contest-detail/contest-detail.component';
import { AdminComponent } from './features/admin/admin.component';
import { AdminContestCreateComponent } from './features/admin/contest-create/admin-contest-create.component';
import { AdminGuard } from './features/admin/admin.guard';
import { FaqComponent } from './features/landing/faq/faq.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'faq', component: FaqComponent },
  { path: 'login', component: LoginRegisterComponent},
  { path: 'test', component: TestComponent },

  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'courses', component: CourseExploreComponent },
      { path: 'courses/create', component: CourseCreateComponent },
      { path: 'courses/:id', redirectTo: '/courses' },
      { path: 'feed', component: HomeComponent },
      { path: 'contests', component: ContestListComponent },
      { path: 'contest/:id', component: ContestDetailComponent },
      { path: 'admin', component: AdminComponent, canActivate: [AdminGuard]  },
      { path: 'admin/contests/create', component: AdminContestCreateComponent, canActivate: [AdminGuard]  },
      { path: 'admin/contests/:id/edit', component: AdminContestCreateComponent, canActivate: [AdminGuard]   },
      { path: 'discussions', component: DiscussionComponent },
      { path: 'discussions/:id', component: DiscussionComponent },
      { path: 'calendar', component: HomeComponent },
      { path: 'messages', component: ChatComponent },
      { path: 'messages/:chatId', component: ChatComponent },
      { path: 'search', component: SearchComponent },
      { path: 'account', redirectTo: 'user/me', pathMatch: 'full' },
      { path: 'user/me', component: UserProfileComponent },
      { path: 'user/:identifier', component: UserProfileComponent },

    ]
  },
  { path: '**', redirectTo: '' }
];
