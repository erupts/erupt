import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Registration',
      status: false
    },
    children: [
      {
        path: 'simple',
        loadChildren: './basic-reg/basic-reg.module#BasicRegModule'
      },
      {
        path: 'header-footer',
        loadChildren: './header-footer-reg/header-footer-reg.module#HeaderFooterRegModule'
      },
      {
        path: 'social',
        loadChildren: './social-reg/social-reg.module#SocialRegModule'
      },
      {
        path: 'social-header-footer',
        loadChildren: './social-header-footer-reg/social-header-footer-reg.module#SocialHeaderFooterRegModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RegistrationRoutingModule { }



