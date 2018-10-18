import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AdvanceWidgetComponent} from './advance-widget.component';

const routes: Routes = [
  {
    path: '',
    component: AdvanceWidgetComponent,
    data: {
      title: 'Advance Widget',
      icon: 'icon-view-grid',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdvanceWidgetRoutingModule { }
