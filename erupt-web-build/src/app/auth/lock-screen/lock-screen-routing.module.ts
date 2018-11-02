import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LockScreenComponent} from './lock-screen.component';

const routes: Routes = [
  {
    path: '',
    component: LockScreenComponent,
    data: {
      title: 'Lock Screen'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LockScreenRoutingModule { }
