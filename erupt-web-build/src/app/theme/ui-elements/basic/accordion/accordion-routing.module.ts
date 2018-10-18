import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AccordionComponent} from './accordion.component';

const routes: Routes = [
  {
    path: '',
    component: AccordionComponent,
    data: {
      title: 'Accordion',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - accordion',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccordionRoutingModule { }
