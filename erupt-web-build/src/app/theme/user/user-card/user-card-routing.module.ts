import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UserCardComponent} from './user-card.component';

const routes: Routes = [
  {
    path: '',
    component: UserCardComponent,
    data: {
      title: 'User Card',
      icon: 'icon-user',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - user card',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserCardRoutingModule { }
