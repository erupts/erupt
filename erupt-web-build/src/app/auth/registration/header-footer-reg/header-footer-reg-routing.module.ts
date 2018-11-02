import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HeaderFooterRegComponent} from './header-footer-reg.component';

const routes: Routes = [
  {
    path: '',
    component: HeaderFooterRegComponent,
    data: {
      title: 'Header & Footer Registration'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HeaderFooterRegRoutingModule { }
