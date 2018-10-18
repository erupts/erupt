import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AnimationComponent} from './animation.component';

const routes: Routes = [
  {
    path: '',
    component: AnimationComponent,
    data: {
      title: 'Animation',
      icon: 'icon-reload rotate-refresh',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - animation',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AnimationRoutingModule { }
