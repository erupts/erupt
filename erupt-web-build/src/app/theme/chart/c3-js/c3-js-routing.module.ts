import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {C3JsComponent} from "./c3-js.component";

const routes: Routes = [
  {
    path: '',
    component: C3JsComponent,
    data: {
      title: 'C3 Chart',
      icon: 'icon-bar-chart-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - c3 chart',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class C3JsRoutingModule { }
