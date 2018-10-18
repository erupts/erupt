import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ComingSoonComponent} from './coming-soon.component';

const routes: Routes = [
  {
    path: '',
    component: ComingSoonComponent,
    data: {
      title: 'Coming Soon',
      icon: 'icon-settings',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - coming soon',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ComingSoonRoutingModule { }
