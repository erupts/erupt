import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PreLoaderComponent} from './pre-loader.component';

const routes: Routes = [
  {
    path: '',
    component: PreLoaderComponent,
    data: {
      title: 'Pre-Loader',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - pre-loader',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PreLoaderRoutingModule { }
