import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabsComponent } from './tabs.component';
import {TabsRoutingModule} from './tabs-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    TabsRoutingModule,
    SharedModule
  ],
  declarations: [TabsComponent]
})
export class TabsModule { }
