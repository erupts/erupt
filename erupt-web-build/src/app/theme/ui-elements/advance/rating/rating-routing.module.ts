import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RatingComponent} from './rating.component';

const routes: Routes = [
  {
    path: '',
    component: RatingComponent,
    data: {
      title: 'Rating',
      icon: 'icon-crown',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - rating',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RatingRoutingModule { }
