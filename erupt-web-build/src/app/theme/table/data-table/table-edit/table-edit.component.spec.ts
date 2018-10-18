import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TableEditComponent } from './table-edit.component';

describe('TableEditComponent', () => {
  let component: TableEditComponent;
  let fixture: ComponentFixture<TableEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TableEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TableEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
