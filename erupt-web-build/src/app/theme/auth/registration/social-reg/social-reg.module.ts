import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SocialRegComponent } from './social-reg.component';
import {SocialRegRoutingModule} from './social-reg-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    SocialRegRoutingModule,
    SharedModule
  ],
  declarations: [SocialRegComponent]
})
export class SocialRegModule { }
