import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotifyComponent } from './notify.component';
import {NotifyRoutingModule} from './notify-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {SimpleNotificationsModule} from 'angular2-notifications';

@NgModule({
  imports: [
    CommonModule,
    NotifyRoutingModule,
    SharedModule,
    SimpleNotificationsModule.forRoot()
  ],
  declarations: [NotifyComponent],
  bootstrap: [NotifyComponent]
})
export class NotifyModule { }
