import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CrmDashboardComponent } from './crm-dashboard.component';

describe('CrmDashboardComponent', () => {
  let component: CrmDashboardComponent;
  let fixture: ComponentFixture<CrmDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CrmDashboardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CrmDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
