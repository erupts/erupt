import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SocialRegComponent} from './social-reg.component';

const routes: Routes = [
  {
    path: '',
    component: SocialRegComponent,
    data: {
      title: 'Social Registration'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SocialRegRoutingModule { }
