import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Advance Components',
      status: false
    },
    children: [
      {
        path: 'modal',
        loadChildren: './modal/modal.module#ModalModule'
      },
      {
        path: 'notifications',
        loadChildren: './notification/notification.module#NotificationModule'
      },
      {
        path: 'notify',
        loadChildren: './notify/notify.module#NotifyModule'
      },
      {
        path: 'rating',
        loadChildren: './rating/rating.module#RatingModule'
      },
      {
        path: 'slider',
        loadChildren: './carousel/carousel.module#CarouselModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdvanceRoutingModule { }
