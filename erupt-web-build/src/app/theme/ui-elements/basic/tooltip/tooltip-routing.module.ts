import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TooltipComponent} from './tooltip.component';

const routes: Routes = [
  {
    path: '',
    component: TooltipComponent,
    data: {
      title: 'Tooltip & Popover',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - tooltip',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TooltipRoutingModule { }
