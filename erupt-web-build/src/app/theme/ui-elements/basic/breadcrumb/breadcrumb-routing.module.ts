import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BreadcrumbComponent} from './breadcrumb.component';

const routes: Routes = [
  {
    path: '',
    component: BreadcrumbComponent,
    data: {
      title: 'Breadcrumbs',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - breadcrumb',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BreadcrumbRoutingModule { }
