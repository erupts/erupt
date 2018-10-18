import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserCardComponent } from './user-card.component';
import {UserCardRoutingModule} from './user-card-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    UserCardRoutingModule,
    SharedModule
  ],
  declarations: [UserCardComponent]
})
export class UserCardModule { }
