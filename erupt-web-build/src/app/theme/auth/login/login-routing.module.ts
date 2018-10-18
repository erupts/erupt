import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Login',
      status: false
    },
    children: [
      {
        path: 'simple',
        loadChildren: './basic-login/basic-login.module#BasicLoginModule'
      },
      {
        path: 'header-footer',
        loadChildren: './header-footer-login/header-footer-login.module#HeaderFooterLoginModule'
      },
      {
        path: 'social',
        loadChildren: './social-login/social-login.module#SocialLoginModule'
      },
      {
        path: 'social-header-footer',
        loadChildren: './social-header-footer-login/social-header-footer-login.module#SocialHeaderFooterLoginModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LoginRoutingModule { }
