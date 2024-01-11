package at.ac.tuwien.kr.alpha.test.util;

import at.ac.tuwien.kr.alpha.api.impl.AlphaFactory;
import at.ac.tuwien.kr.alpha.core.actions.ActionImplementationProvider;
import at.ac.tuwien.kr.alpha.test.MockActionImplementationProvider;

public class MockedActionsAlphaFactory extends AlphaFactory {

	private MockActionImplementationProvider actionImplementationMock = new MockActionImplementationProvider("");

	public MockActionImplementationProvider getActionImplementationMock() {
		return actionImplementationMock;
	}

	public void setActionImplementationMock(MockActionImplementationProvider actionImplementationMock) {
		this.actionImplementationMock = actionImplementationMock;
	}

	protected ActionImplementationProvider newActionImplementationProvider() {
		return actionImplementationMock;
	}
	
}
