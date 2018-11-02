import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SocialHeaderFooterRegComponent} from './social-header-footer-reg.component';

const routes: Routes = [
  {
    path: '',
    component: SocialHeaderFooterRegComponent,
    data: {
      title: 'Social Header & Footer Registration'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SocialHeaderFooterRegRoutingModule { }
