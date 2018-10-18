import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Bootstrap Table',
      status: false
    },
    children: [
      {
        path: 'basic',
        loadChildren: './table-basic/table-basic.module#TableBasicModule'
      },
      {
        path: 'sizing',
        loadChildren: './sizing/sizing.module#SizingModule'
      },
      {
        path: 'border',
        loadChildren: './border/border.module#BorderModule'
      },
      {
        path: 'styling',
        loadChildren: './styling/styling.module#StylingModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BootstrapTableRoutingModule { }
