import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SocialHeaderFooterLoginComponent} from './social-header-footer-login.component';

const routes: Routes = [
  {
    path: '',
    component: SocialHeaderFooterLoginComponent,
    data: {
      title: 'Social Header & Footer Login'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SocialHeaderFooterLoginRoutingModule { }
