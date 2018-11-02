import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SocialLoginComponent} from './social-login.component';

const routes: Routes = [
  {
    path: '',
    component: SocialLoginComponent,
    data: {
      title: 'Social Login'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SocialLoginRoutingModule { }
