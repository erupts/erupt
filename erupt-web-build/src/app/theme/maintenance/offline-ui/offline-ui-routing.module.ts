import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {OfflineUiComponent} from './offline-ui.component';

const routes: Routes = [
  {
    path: '',
    component: OfflineUiComponent,
    data: {
      title: 'Offliine UI',
      icon: 'icon-settings',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - offline ui',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OfflineUiRoutingModule { }
