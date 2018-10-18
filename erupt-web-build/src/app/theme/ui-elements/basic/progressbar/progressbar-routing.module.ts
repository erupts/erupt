import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProgressbarComponent} from './progressbar.component';

const routes: Routes = [
  {
    path: '',
    component: ProgressbarComponent,
    data: {
      title: 'Progressbar',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - progressbar',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProgressbarRoutingModule { }
