import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskIssueComponent } from './task-issue.component';

describe('TaskIssueComponent', () => {
  let component: TaskIssueComponent;
  let fixture: ComponentFixture<TaskIssueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TaskIssueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskIssueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
